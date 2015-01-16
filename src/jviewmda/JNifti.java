package jviewmda;

import java.io.IOException;
import niftijio.NiftiHeader;
import niftijio.NiftiVolume;

/**
 *
 * @author magland
 */
public class JNifti {

	Mda m_array = new Mda();
	NiftiHeader m_header = new NiftiHeader();

	JNifti() {
	}

	public void read(String path) throws IOException {
		NiftiVolume X = NiftiVolume.read(path);
		double[][][][] data = X.data;
		m_header = X.header;
		int N1 = data.length;
		int N2 = data[0].length;
		int N3 = data[0][0].length;
		int N4 = data[0][0][0].length;
		m_array.allocate(N1, N2, N3, N4);
		for (int t = 0; t < N4; t++) {
			for (int z = 0; z < N3; z++) {
				for (int y = 0; y < N2; y++) {
					for (int x = 0; x < N1; x++) {
						m_array.setValue(data[x][y][z][t], x, y, z, t);
					}
				}
			}
		}
	}

	public Mda array() {
		return m_array;
	}
}
